const express = require("express");
const router = express.Router();
const User = require("../models/User");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { sendOTP } = require("../utils/mailer");
const RankBenefit = require("../models/RankBenefit");
const Promotion = require("../models/Promotion");
const Blog = require("../models/Blog");

// Generate 6 digit OTP
const generateOTP = () => Math.floor(100000 + Math.random() * 900000).toString();

// 1. Send OTP for Registration
router.post("/send-otp-reg", async (req, res) => {
    try {
        const { email } = req.body;
        let user = await User.findOne({ email });

        if (user && user.isVerified) {
            return res.status(400).json({ success: false, message: "Email đã được đăng ký" });
        }

        const otp = generateOTP();
        const otpExpires = new Date(Date.now() + 10 * 60 * 1000);

        if (user) {
            user.otp = otp;
            user.otpExpires = otpExpires;
            await user.save();
        } else {
            user = new User({
                fullName: "User",
                email,
                password: "temporary_password", // Will be updated in finalize
                otp,
                otpExpires,
                isVerified: false
            });
            await user.save();
        }

        await sendOTP(email, otp);
        res.json({ success: true, message: "Mã OTP đã được gửi vào email của bạn" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Send OTP for Payment Verification
router.post("/send-payment-otp", async (req, res) => {
    try {
        const { email } = req.body;
        if (!email) return res.status(400).json({ success: false, message: "Thiếu email" });

        const otp = generateOTP();
        // Just send it to email
        await sendOTP(email, otp);
        // We put the OTP in message field so the app can "know" it for simulation
        res.json({ success: true, message: otp });
    } catch (error) {
        console.error(error);
        res.status(500).json({ success: false });
    }
});

// 2. Verify OTP
router.post("/verify-otp", async (req, res) => {
    try {
        const { email, otp } = req.body;
        const user = await User.findOne({ email });

        if (!user || user.otp !== otp || user.otpExpires < new Date()) {
            return res.status(400).json({ success: false, message: "Mã OTP không hợp lệ hoặc đã hết hạn" });
        }

        // We don't mark isVerified=true yet, we wait for password
        res.json({ success: true, message: "Xác thực OTP thành công" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// 3. Finalize Registration
router.post("/register-finalize", async (req, res) => {
    try {
        const { email, password, name, phone, cccd, passport, dob, country, title, address } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ success: false, message: "Không tìm thấy yêu cầu đăng ký" });
        }

        // Helper function to set null if empty
        const nullIfEmpty = (val) => (val && typeof val === "string" && val.trim() !== "") ? val.trim() : null;

        user.password = await bcrypt.hash(password, 10);
        user.fullName = name || user.fullName;
        user.phone = nullIfEmpty(phone) || user.phone;
        user.cccd = nullIfEmpty(cccd);
        user.passport = nullIfEmpty(passport);
        user.dob = nullIfEmpty(dob);
        user.country = nullIfEmpty(country);
        user.title = nullIfEmpty(title);
        user.address = nullIfEmpty(address);

        // Generate random member code (Format: XXXX XXXX XXXX)
        const part1 = Math.floor(1000 + Math.random() * 9000);
        const part2 = Math.floor(1000 + Math.random() * 9000);
        const part3 = Math.floor(1000 + Math.random() * 9000);
        user.memberCode = `${part1} ${part2} ${part3}`;

        user.isVerified = true;
        user.otp = undefined;
        user.otpExpires = undefined;
        await user.save();

        const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" });
        res.json({
            success: true,
            message: "Đăng ký tài khoản thành công",
            token,
            user: { id: user._id, email: user.email, name: user.fullName }
        });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Login
router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });

        if (!user || !user.isVerified) {
            return res.status(400).json({ success: false, message: "Email hoặc mật khẩu không đúng hoặc tài khoản chưa xác thực" });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ success: false, message: "Email hoặc mật khẩu không đúng" });
        }

        const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" });
        res.json({
            success: true,
            message: "Đăng nhập thành công",
            token,
            user: { id: user._id, email: user.email, name: user.fullName }
        });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Forgot Password (Keep as is)
router.post("/forgot-password", async (req, res) => {
    try {
        const { email } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ success: false, message: "Email không tồn tại" });
        }

        const otp = generateOTP();
        user.otp = otp;
        user.otpExpires = new Date(Date.now() + 10 * 60 * 1000);
        await user.save();

        await sendOTP(email, otp);
        res.json({ success: true, message: "Mã OTP đã được gửi vào email" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Reset Password (Keep as is)
router.post("/reset-password", async (req, res) => {
    try {
        const { email, otp, newPassword } = req.body;
        const user = await User.findOne({ email });

        if (!user || user.otp !== otp || user.otpExpires < new Date()) {
            return res.status(400).json({ success: false, message: "Mã OTP không hợp lệ hoặc đã hết hạn" });
        }

        user.password = await bcrypt.hash(newPassword, 10);
        user.otp = undefined;
        user.otpExpires = undefined;
        user.isVerified = true;
        await user.save();

        res.json({ success: true, message: "Đổi mật khẩu thành công" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Get User Profile
router.get("/profile", async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return res.status(401).json({ success: false, message: "Không có quyền truy cập" });
        }

        const token = authHeader.split(" ")[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        const user = await User.findById(decoded.id).select("-password -otp -otpExpires");
        if (!user) {
            return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
        }

        // Đảm bảo trả về JSON "phẳng" cho Android
        const userObj = user.toObject();
        const responseData = {
            ...userObj,
            _id: user._id.toString(),
            createdAt: user.createdAt ? user.createdAt.toISOString() : null,
            updatedAt: user.updatedAt ? user.updatedAt.toISOString() : null
        };

        res.status(200).json(responseData);
    } catch (error) {
        console.error("Profile Error:", error);
        res.status(401).json({ success: false, message: "Xác thực không hợp lệ" });
    }
});

// Get Rank Benefits
router.get("/rank-benefits", async (req, res) => {
    try {
        let { rank } = req.query;
        if (!rank) rank = "Đồng";

        // Tìm kiếm không phân biệt hoa thường
        const benefits = await RankBenefit.find({
            rank: { $regex: new RegExp("^" + rank + "$", "i") }
        });
        res.json(benefits);
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Get 3 newest promotions
router.get("/promotions", async (req, res) => {
    try {
        const promotions = await Promotion.find({ status: "Active" })
            .sort({ createdAt: -1 })
            .limit(3);

        // Chuyển đổi dữ liệu để App dễ đọc và gắn full URL cho ảnh
        const formattedPromotions = promotions.map(p => {
            const baseUrl = req.protocol + '://' + req.get('host');
            return {
                title: p.promotionName,
                endDate: p.endDate ? new Date(p.endDate).toLocaleDateString('vi-VN') : "",
                imageUrl: p.imageUrl.startsWith('http') ? p.imageUrl : (baseUrl + p.imageUrl)
            };
        });

        res.json(formattedPromotions);
    } catch (error) {
        console.error("Promo error:", error);
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Get 2 newest destination blogs
router.get("/destination-blogs", async (req, res) => {
    try {
        const blogs = await Blog.find({
            status: "published",
            categorySlug: "diem-den"
        })
        .sort({ createdAt: -1 })
        .limit(2);

        const formattedBlogs = blogs.map(b => {
            const baseUrl = req.protocol + '://' + req.get('host');
            return {
                title: b.destination || b.title,
                description: b.shortDescription,
                imageUrl: b.thumbnailUrl.startsWith('http') ? b.thumbnailUrl : (baseUrl + b.thumbnailUrl)
            };
        });

        res.json(formattedBlogs);
    } catch (error) {
        console.error("Blog error:", error);
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

module.exports = router;

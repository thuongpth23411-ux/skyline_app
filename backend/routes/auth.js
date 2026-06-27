const express = require("express");
const router = express.Router();
const User = require("../models/User");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { sendOTP } = require("../utils/mailer");

// Generate 6 digit OTP
const generateOTP = () => Math.floor(100000 + Math.random() * 900000).toString();

// Register
router.post("/register", async (req, res) => {
    try {
        const { email, password, name, phone } = req.body;

        let user = await User.findOne({ email });
        if (user && user.isVerified) {
            return res.status(400).json({ success: false, message: "Email đã được đăng ký" });
        }

        const hashedPassword = await bcrypt.hash(password, 10);
        const otp = generateOTP();
        const otpExpires = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

        if (user) {
            // Update existing unverified user
            user.password = hashedPassword;
            user.fullName = name || "User";
            user.phone = phone;
            user.otp = otp;
            user.otpExpires = otpExpires;
            await user.save();
        } else {
            // Create new user
            user = new User({
                fullName: name || "User",
                email,
                password: hashedPassword,
                phone,
                otp,
                otpExpires
            });
            await user.save();
        }

        await sendOTP(email, otp);
        res.json({ success: true, message: "Đăng ký thành công, vui lòng kiểm tra email để lấy mã OTP" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Verify OTP
router.post("/verify-otp", async (req, res) => {
    try {
        const { email, otp } = req.body;
        const user = await User.findOne({ email });

        if (!user || user.otp !== otp || user.otpExpires < new Date()) {
            return res.status(400).json({ success: false, message: "Mã OTP không hợp lệ hoặc đã hết hạn" });
        }

        user.isVerified = true;
        user.otp = undefined;
        user.otpExpires = undefined;
        await user.save();

        res.json({ success: true, message: "Xác thực thành công" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Login
router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(400).json({ success: false, message: "Email hoặc mật khẩu không đúng" });
        }

        if (!user.isVerified) {
            const otp = generateOTP();
            user.otp = otp;
            user.otpExpires = new Date(Date.now() + 10 * 60 * 1000);
            await user.save();
            await sendOTP(email, otp);
            return res.status(400).json({ success: false, message: "Tài khoản chưa xác thực. Mã OTP mới đã được gửi vào email." });
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

// Forgot Password
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

// Reset Password
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

module.exports = router;

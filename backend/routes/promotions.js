const express = require("express");
const router = express.Router();
const Promotion = require("../models/Promotion");
const UserVoucher = require("../models/UserVoucher");
const jwt = require("jsonwebtoken");

// Get all
router.get("/", async (req, res) => {
    try {
        const promotions = await Promotion.find();
        const cleaned = promotions.map(p => {
            const obj = p.toObject();
            obj._id = p._id.toString();
            return obj;
        });
        res.json(cleaned);
    } catch (e) {
        res.status(500).json({ success: false });
    }
});

// Toggle Save
router.post("/toggle-save", async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) return res.status(401).json({ success: false, message: "No token" });

        const token = authHeader.split(" ")[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        const userId = decoded.id;
        const { promotionId } = req.body;

        const existing = await UserVoucher.findOne({ userId, promotionId });
        if (existing) {
            await UserVoucher.deleteOne({ _id: existing._id });
            return res.json({ success: true, message: "Đã bỏ lưu voucher" });
        } else {
            const userVoucher = new UserVoucher({ userId, promotionId });
            await userVoucher.save();
            return res.json({ success: true, message: "Đã lưu voucher thành công" });
        }
    } catch (e) {
        res.status(500).json({ success: false });
    }
});

// My Vouchers
router.get("/my-vouchers", async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) return res.status(401).json({ success: false });

        const token = authHeader.split(" ")[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        const userId = decoded.id;

        const saved = await UserVoucher.find({ userId }).populate("promotionId");
        const list = saved.map(s => {
            if (!s.promotionId) return null;
            const p = s.promotionId.toObject();
            p._id = s.promotionId._id.toString();
            return p;
        }).filter(p => p !== null);

        res.json(list);
    } catch (e) {
        res.status(500).json({ success: false });
    }
});

module.exports = router;

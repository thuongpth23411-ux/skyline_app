const express = require("express");
const router = express.Router();
const Airline = require("../models/Airline");

// Get all airlines
router.get("/", async (req, res) => {
    try {
        const airlines = await Airline.find({ isActive: true });
        res.json(airlines);
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách hãng hàng không" });
    }
});

module.exports = router;

const express = require("express");
const router = express.Router();
const Airport = require("../models/Airport");

// Get all airports
router.get("/", async (req, res) => {
    try {
        const airports = await Airport.find();
        res.json(airports);
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách sân bay" });
    }
});

module.exports = router;

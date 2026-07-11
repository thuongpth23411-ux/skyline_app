const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use("/images", express.static("images"));

// Routes
const authRoutes = require("./routes/auth");
const airportRoutes = require("./routes/airports");
const flightRoutes = require("./routes/flights");
const promotionRoutes = require("./routes/promotions");
const ticketRoutes = require("./routes/tickets");
const airlineRoutes = require("./routes/airlines");
const blogRoutes = require("./routes/blogs");

app.use("/api/auth", authRoutes);
app.use("/api/airports", airportRoutes);
app.use("/api/flights", flightRoutes);
app.use("/api/promotions", promotionRoutes);
app.use("/api/airlines", airlineRoutes);
app.use("/api/tickets", ticketRoutes);
app.use("/api/blogs", blogRoutes);

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)
.then(async () => {
    console.log("✅ Connected MongoDB");

    // Seed Rank Benefits (Chỉ seed nếu trống)
    const RankBenefit = require("./models/RankBenefit");
    const count = await RankBenefit.countDocuments();
    if (count < 3) {
        await RankBenefit.insertMany([
            { rank: "Đồng", title: "Tích điểm cơ bản", description: "Tích lũy điểm thưởng cho mỗi chuyến bay.", iconType: "points" },
            { rank: "Bạc", title: "Tích điểm 1.2x", description: "Nhận thêm 20% điểm thưởng.", iconType: "points" },
            { rank: "Vàng", title: "Phòng chờ hạng thương gia", description: "Tận hưởng không gian sang trọng.", iconType: "checkin" }
        ]);
        console.log("🚀 Rank Benefits Seeded");
    }

    // KHÔNG TỰ ĐỘNG SEED PROMOTIONS NỮA ĐỂ TRÁNH MẤT DATA CỦA BẠN
    console.log("ℹ️ Database seeding is now manual or conditional.");
})
.catch((err) => {
    console.error("❌ MongoDB connection error:", err);
});

app.get("/", (req, res) => {
    res.send("Skyline API is running...");
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`✅ Server is running on port ${PORT}`);
});

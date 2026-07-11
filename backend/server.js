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
const passengerRoutes = require("./routes/passengers");

app.use("/api/auth", authRoutes);
app.use("/api/airports", airportRoutes);
app.use("/api/flights", flightRoutes);
app.use("/api/promotions", promotionRoutes);
app.use("/api/airlines", airlineRoutes);
app.use("/api/tickets", ticketRoutes);
app.use("/api/blogs", blogRoutes);
app.use("/api/passenger-directory", passengerRoutes);

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)
.then(async () => {
    console.log("✅ Connected MongoDB");
    console.log("✅ Connected MongoDB");

    // Seed Rank Benefits (Chỉ seed nếu trống)
    const RankBenefit = require("./models/RankBenefit");
    const countRank = await RankBenefit.countDocuments();
    if (countRank < 3) {
        await RankBenefit.insertMany([
            { rank: "Đồng", title: "Tích điểm cơ bản", description: "Tích lũy điểm thưởng cho mỗi chuyến bay.", iconType: "points" },
            { rank: "Bạc", title: "Tích điểm 1.2x", description: "Nhận thêm 20% điểm thưởng.", iconType: "points" },
            { rank: "Vàng", title: "Phòng chờ hạng thương gia", description: "Tận hưởng không gian sang trọng.", iconType: "checkin" }
        ]);
        console.log("🚀 Rank Benefits Seeded");
    }

    // TỰ ĐỘNG SEED PROMOTIONS NẾU TRỐNG
    const Promotion = require("./models/Promotion");
    const countPromo = await Promotion.countDocuments();
    if (countPromo === 0) {
        await Promotion.insertMany([
            {
                promotionId: "PROMO001",
                promotionCode: "SKYLINE20",
                promotionName: "Chào hè rạng rỡ",
                description: "Giảm 20% cho tất cả các chuyến bay nội địa",
                promotionCategory: "EXCLUSIVE",
                discountType: "PERCENT",
                discountValue: 20,
                maxDiscount: 200000,
                minimumOrder: 500000,
                startDate: new Date(),
                endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
                quantity: 100,
                status: "Active"
            },
            {
                promotionId: "PROMO002",
                promotionCode: "NEWUSER",
                promotionName: "Chào bạn mới",
                description: "Tặng 50k cho lần đầu đặt vé",
                promotionCategory: "NEW_USER",
                discountType: "FIXED",
                discountValue: 50000,
                minimumOrder: 0,
                startDate: new Date(),
                endDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000),
                quantity: 1000,
                status: "Active"
            }
        ]);
        console.log("🚀 Promotions Seeded Automatically");
    }
    // KHẮC PHỤC LỖI DUPLICATE KEY: Bỏ index unique cho bookingCode vì 1 code dùng cho nhiều vé (khứ hồi)
    try {
        await mongoose.connection.collection("tickets").dropIndex("bookingCode_1");
        console.log("🗑️ Dropped unique index on bookingCode");
    } catch (e) {
        // Index might not exist or already dropped
    }

    // Seed Rank Benefits (Chỉ seed nếu trống)

app.get("/", (req, res) => {
    res.send("Skyline API is running...");
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`✅ Server is running on port ${PORT}`);
});

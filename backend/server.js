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

    // Seed Rank Benefits
    const RankBenefit = require("./models/RankBenefit");
    const count = await RankBenefit.countDocuments();
    if (count < 5) {
        await RankBenefit.deleteMany({});
        await RankBenefit.insertMany([
            { rank: "Đồng", title: "Tích điểm cơ bản", description: "Tích lũy điểm thưởng cho mỗi chuyến bay.", iconType: "points" },
            { rank: "Bạc", title: "Tích điểm 1.2x", description: "Nhận thêm 20% điểm thưởng.", iconType: "points" },
            { rank: "Vàng", title: "Phòng chờ hạng thương gia", description: "Tận hưởng không gian sang trọng.", iconType: "checkin" }
        ]);
    }

    // Seed chuẩn theo cấu trúc thực tế của bạn
    const Promotion = require("./models/Promotion");
    await Promotion.deleteMany({});

    const categories = ["Thành viên", "Độc quyền", "Thanh toán", "Khách hàng mới"];
    const dbCats = ["MEMBER", "EXCLUSIVE", "PAYMENT", "NEW_USER"];
    const seedData = [];

    dbCats.forEach((dbCat, index) => {
        const displayCat = categories[index];
        for (let i = 1; i <= 5; i++) {
            seedData.push({
                promotionId: `PROMO${index}${i}`,
                promotionCode: `${dbCat}${i}${10 + i}`,
                promotionName: `${displayCat} - Voucher ${i}`,
                description: `Chào mừng bạn đến với chương trình ưu đãi của Skyline!\n\nChi tiết ưu đãi:\n• Giảm giá trực tiếp ${i % 2 === 0 ? "200.000 VNĐ" : "15%"} cho mỗi đơn hàng.\n• Áp dụng cho toàn bộ các chặng bay nội địa và quốc tế.\n• Không áp dụng đồng thời với các chương trình khác.\n\nChúc bạn có những chuyến bay tuyệt vời cùng Skyline!`,
                promotionCategory: dbCat,
                imageUrl: "https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=800",
                discountType: i % 2 === 0 ? "FIXED" : "PERCENT",
                discountValue: i % 2 === 0 ? 200000 : 15,
                maxDiscount: 100000,
                minimumOrder: 500000,
                startDate: new Date("2026-01-01"),
                endDate: new Date("2026-12-31"),
                quantity: 500,
                applicableAirline: "ALL",
                status: "Active"
            });
        }
    });

    await Promotion.insertMany(seedData);
    console.log("🚀 DATABASE RE-SEEDED: With Beautiful Descriptions!");
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

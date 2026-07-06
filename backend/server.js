const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
const authRoutes = require("./routes/auth");
const airportRoutes = require("./routes/airports");
const flightRoutes = require("./routes/flights");

app.use("/api/auth", authRoutes);
app.use("/api/airports", airportRoutes);
app.use("/api/flights", flightRoutes);

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)
.then(async () => {
    console.log("✅ Connected MongoDB");

    // Seed initial Rank Benefits if empty
    const RankBenefit = require("./models/RankBenefit");
    const count = await RankBenefit.countDocuments();
    if (count < 5) { // Nếu thiếu dữ liệu thì nạp thêm/nạp lại
        await RankBenefit.deleteMany({}); // Xóa cũ nạp mới cho đồng bộ
        await RankBenefit.insertMany([
            { rank: "Đồng", title: "Tích điểm cơ bản", description: "Tích lũy điểm thưởng cho mỗi chuyến bay.", iconType: "points" },
            { rank: "Đồng", title: "Thông báo khuyến mãi", description: "Nhận thông tin ưu đãi sớm nhất qua email.", iconType: "notifications" },
            { rank: "Bạc", title: "Tích điểm 1.2x", description: "Nhận thêm 20% điểm thưởng cho mỗi giao dịch thành công.", iconType: "points" },
            { rank: "Bạc", title: "Ưu tiên check-in", description: "Tiết kiệm thời gian với quầy dịch vụ dành riêng cho thành viên Bạc.", iconType: "checkin" },
            { rank: "Bạc", title: "Voucher sinh nhật 100k", description: "Món quà đặc biệt vào tháng sinh nhật của bạn.", iconType: "voucher" },
            { rank: "Vàng", title: "Tích điểm 1.5x", description: "Nhận thêm 50% điểm thưởng cho mỗi giao dịch thành công.", iconType: "points" },
            { rank: "Vàng", title: "Phòng chờ hạng thương gia", description: "Tận hưởng không gian sang trọng và tiện nghi trước chuyến bay.", iconType: "checkin" },
            { rank: "Vàng", title: "Ưu tiên chọn chỗ ngồi", description: "Miễn phí chọn chỗ ngồi ưa thích trên mọi chuyến bay.", iconType: "voucher" }
        ]);
        console.log("🌱 Seeded/Updated Rank Benefits");
    }
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

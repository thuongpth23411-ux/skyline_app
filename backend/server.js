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
app.use("/api/auth", authRoutes);

// MongoDB Connection
const User = require("./models/User");
const bcrypt = require("bcryptjs");

mongoose.connect(process.env.MONGO_URI)
.then(async () => {
    console.log("✅ Connected MongoDB");

    // Tạo tài khoản test nếu chưa có
    const testEmail = "test@gmail.com";
    const existingUser = await User.findOne({ email: testEmail });
    if (!existingUser) {
        const hashedPassword = await bcrypt.hash("Password123!", 10);
        await User.create({
            fullName: "Test User",
            email: testEmail,
            password: hashedPassword,
            isVerified: true
        });
        console.log("✅ Created test account: test@gmail.com / Password123!");
    }
})
.catch((err) => {
    console.error("❌ MongoDB connection error:", err);
});

app.get("/", (req, res) => {
    res.send("Skyline API is running...");
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

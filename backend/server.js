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
.then(() => {
    console.log("✅ Connected MongoDB");
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

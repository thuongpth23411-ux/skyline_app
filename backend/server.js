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
    console.log("🚀 Server is ready. Route /api/passenger-directory is ACTIVE.");
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

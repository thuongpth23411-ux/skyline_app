const express = require("express");
const router = express.Router();
const Flight = require("../models/Flight");
const Airline = require("../models/Airline");
const Airport = require("../models/Airport");
const FlightSeat = require("../models/FlightSeat");

router.post("/search", async (req, res) => {
    try {
        const { fromCode, toCode, date } = req.body;

        if (!fromCode || !toCode || !date) {
            return res.status(400).json({ success: false, message: "Thiếu thông tin tìm kiếm" });
        }

        console.log(`🔍 Searching: ${fromCode} -> ${toCode} on ${date}`);

        // Escape date for RegExp safety
        const safeDate = date.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

        const flights = await Flight.find({
            fromAirportId: fromCode,
            toAirportId: toCode,
            departureDate: { $regex: new RegExp(`^${safeDate}`) }
        }).lean();

        for (let flight of flights) {
            // Fetch related data with fallback to empty objects
            flight.airline = (await Airline.findOne({ airlineId: flight.airlineId })) || { airlineName: "Skyline Airways" };
            flight.departureAirport = (await Airport.findOne({ airportId: flight.fromAirportId })) || { code: fromCode, city: "Unknown" };
            flight.arrivalAirport = (await Airport.findOne({ airportId: flight.toAirportId })) || { code: toCode, city: "Unknown" };

            // Calculate basePrice from priceOptions array
            if (flight.priceOptions && Array.isArray(flight.priceOptions) && flight.priceOptions.length > 0) {
                const prices = flight.priceOptions.map(opt => {
                    let val = (typeof opt === 'object' && opt !== null) ? (opt.totalPrice || opt.price || 0) : opt;
                    if (typeof val === 'string') {
                        val = val.replace(/[^0-9]/g, '');
                    }
                    return parseFloat(val);
                }).filter(p => !isNaN(p) && p > 0);

                flight.basePrice = prices.length > 0 ? Math.min(...prices) : 0;
            } else {
                flight.basePrice = 0;
            }
        }

        console.log(`✅ Found ${flights.length} flights`);
        res.json(flights);
    } catch (error) {
        console.error("❌ Search error details:", error);
        res.status(500).json({ success: false, message: "Lỗi hệ thống khi tìm kiếm chuyến bay" });
    }
});

// Lấy danh sách ghế của một chuyến bay
router.get("/:flightId/seats", async (req, res) => {
    try {
        const seats = await FlightSeat.find({ flightId: req.params.flightId }).sort({ rowNumber: 1, seatColumn: 1 });
        res.json(seats);
    } catch (error) {
        console.error("❌ Seat fetch error:", error);
        res.status(500).json({ success: false, message: "Lỗi khi lấy sơ đồ ghế" });
    }
});

module.exports = router;

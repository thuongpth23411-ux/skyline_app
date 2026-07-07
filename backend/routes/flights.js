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

        const flights = await Flight.find({
            fromAirportId: fromCode,
            toAirportId: toCode,
            departureDate: { $regex: new RegExp(`^${date}`) }
        }).lean();

        for (let flight of flights) {
            flight.airline = await Airline.findOne({ airlineId: flight.airlineId });
            flight.departureAirport = await Airport.findOne({ airportId: flight.fromAirportId });
            flight.arrivalAirport = await Airport.findOne({ airportId: flight.toAirportId });

            if (flight.priceOptions && Array.isArray(flight.priceOptions)) {
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
        console.error("❌ Search error:", error);
        res.status(500).json({ success: false, message: "Lỗi hệ thống khi tìm kiếm chuyến bay" });
    }
});

// Lấy danh sách ghế của một chuyến bay
router.get("/:flightId/seats", async (req, res) => {
    try {
        const seats = await FlightSeat.find({ flightId: req.params.flightId }).sort({ rowNumber: 1, seatColumn: 1 });
        res.json(seats);
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi khi lấy sơ đồ ghế" });
    }
});

module.exports = router;
const express = require("express");
const router = express.Router();
const Flight = require("../models/Flight");
const Airline = require("../models/Airline");
const Airport = require("../models/Airport");

// Search flights
router.post("/search", async (req, res) => {
    try {
        const { fromCode, toCode, date } = req.body;

        console.log(`Searching: from ${fromCode} to ${toCode} on ${date}`);

        // Find flights based on IDs provided (assuming fromCode/toCode match fromAirportId/toAirportId)
        const flights = await Flight.find({
            fromAirportId: fromCode,
            toAirportId: toCode,
            departureDate: date
        }).lean();

        // Populate airline and airport info manually since we're using String IDs instead of ObjectIds
        for (let flight of flights) {
            flight.airline = await Airline.findOne({ airlineId: flight.airlineId });
            flight.departureAirport = await Airport.findOne({ airportId: flight.fromAirportId });
            flight.arrivalAirport = await Airport.findOne({ airportId: flight.toAirportId });

            // Get lowest price from priceOptions
            if (flight.priceOptions && flight.priceOptions.length > 0) {
                flight.basePrice = Math.min(...flight.priceOptions.map(option => option.price));
            } else {
                flight.basePrice = 0;
            }
        }

        console.log(`Found ${flights.length} flights`);
        res.json(flights);
    } catch (error) {
        console.error("Search error:", error);
        res.status(500).json({ success: false, message: "Lỗi máy chủ khi tìm kiếm chuyến bay" });
    }
});

module.exports = router;

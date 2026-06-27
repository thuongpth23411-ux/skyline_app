const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const app = express();

app.use(cors());
app.use(express.json());

mongoose.connect(process.env.MONGO_URI)
.then(async () => {
    console.log("✅ Connected MongoDB");

    await createSampleUser();
})
.catch((err) => {
    console.log(err);
});

app.get("/", (req, res) => {
    res.send("Skyline API is running...");
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

//Testtttt
const User = require("./models/User");

async function createSampleUser() {
    const count = await User.countDocuments();

    if (count === 0) {
        await User.create({
            fullName: "Admin",
            email: "admin@gmail.com",
            password: "123456",
            phone: "0123456789"
        });

        console.log("✅ Sample user created");
    }
}

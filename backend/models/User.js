const mongoose = require("mongoose");

const userSchema = new mongoose.Schema(
{
    fullName: {
        type: String,
        required: true
    },

    email: {
        type: String,
        required: true,
        unique: true
    },

    password: {
        type: String,
        required: true
    },

    phone: {
        type: String
    },

    cccd: {
        type: String
    },

    passport: {
        type: String
    },

    dob: {
        type: String
    },

    country: {
        type: String
    },

    title: {
        type: String
    },

    address: {
        type: String
    },

    memberCode: {
        type: String
    },

    avatarUrl: {
        type: String,
        default: ""
    },

    rank: {
        type: String,
        default: "Đồng"
    },

    skyPoints: {
        type: Number,
        default: 0
    },

    isVerified: {
        type: Boolean,
        default: false
    },

    otp: {
        type: String
    },

    otpExpires: {
        type: Date
    }
},
{
    timestamps: true
});

module.exports = mongoose.model("User", userSchema);
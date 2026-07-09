const mongoose = require("mongoose");

const blogSchema = new mongoose.Schema({
    blogCode: { type: String, unique: true },
    title: { type: String, required: true },
    slug: { type: String },
    category: { type: String },
    destination: { type: String },
    destinationCode: { type: String },
    shortDescription: { type: String },
    thumbnailUrl: { type: String },
    coverImageUrl: { type: String },
    status: { type: String, default: "published" },
    isFeatured: { type: Boolean, default: false }
}, { timestamps: true, collection: 'blogs' });

module.exports = mongoose.model("Blog", blogSchema);

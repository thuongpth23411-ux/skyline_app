const express = require("express");
const router = express.Router();
const Blog = require("../models/Blog");

// Get all published blogs
router.get("/", async (req, res) => {
    try {
        const blogs = await Blog.find({ status: "published" }).sort({ sortOrder: 1, createdAt: -1 });
        res.json(blogs);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get featured blogs
router.get("/featured", async (req, res) => {
    try {
        const blogs = await Blog.find({ isFeatured: true, status: "published" }).sort({ sortOrder: 1 });
        res.json(blogs);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get blog by slug or blogCode
router.get("/:identifier", async (req, res) => {
    try {
        const blog = await Blog.findOne({
            $or: [{ slug: req.params.identifier }, { blogCode: req.params.identifier }],
            status: "published"
        });
        if (!blog) return res.status(404).json({ message: "Blog not found" });
        res.json(blog);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;

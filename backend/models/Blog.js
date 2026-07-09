const mongoose = require("mongoose");

const QuickInfoSchema = new mongoose.Schema({
  quickInfoId: String,
  icon: String,
  title: String,
  value: String,
  sortOrder: Number
});

const SectionItemSchema = new mongoose.Schema({
  itemId: String,
  title: String,
  subtitle: String,
  description: String,
  imageUrl: String,
  icon: String,
  bulletPoints: [String],
  sortOrder: Number
});

const SectionSchema = new mongoose.Schema({
  sectionId: String,
  sectionNumber: Number,
  title: String,
  type: String, // reason_grid, place_grid, food_grid, tips, itinerary
  sortOrder: Number,
  items: [SectionItemSchema]
});

const BlogSchema = new mongoose.Schema({
  blogCode: { type: String, required: true, unique: true },
  title: { type: String, required: true },
  slug: { type: String, required: true, unique: true },
  category: String,
  categorySlug: String,
  destination: String,
  destinationCode: String,
  shortDescription: String,
  introContent: String,
  thumbnailUrl: String,
  coverImageUrl: String,
  author: {
    authorId: String,
    name: String,
    avatarUrl: String
  },
  publishedDate: Date,
  readTime: String,
  likesCount: { type: Number, default: 0 },
  viewsCount: { type: Number, default: 0 },
  quickInfos: [QuickInfoSchema],
  sections: [SectionSchema],
  cta: {
    text: String,
    action: String,
    destinationCode: String
  },
  isFeatured: { type: Boolean, default: false },
  status: { type: String, enum: ["draft", "published"], default: "published" },
  sortOrder: Number
}, { timestamps: true });

module.exports = mongoose.model("Blog", BlogSchema);

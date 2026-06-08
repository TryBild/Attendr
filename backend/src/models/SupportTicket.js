import mongoose from "mongoose";

const supportTicketSchema = new mongoose.Schema({
  ticketId:         { type: String, unique: true },
  mobile:           { type: String },
  teamId:           { type: String },
  issueDescription: { type: String, required: true },
  screenshotUrls:   [{ type: String }],
  deviceInfo: {
    platform:   String,
    userAgent:  String,
    appVersion: String,
  },
  status: {
    type: String,
    enum: ["open", "in-progress", "resolved", "closed"],
    default: "open",
  },
  articleSlug: { type: String },
}, { timestamps: true });

export default mongoose.model("SupportTicket", supportTicketSchema);

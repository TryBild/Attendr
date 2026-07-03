import { z } from "zod";

// Field names match the live contract: action checkin/checkout, mockDetected.
export const attendanceMarkSchema = z.object({
  action:       z.enum(["checkin", "checkout"], { error: "action must be checkin or checkout" }),
  latitude:     z.number({ error: "latitude and longitude are required" }).min(-90).max(90),
  longitude:    z.number({ error: "latitude and longitude are required" }).min(-180).max(180),
  mockDetected: z.boolean().optional().default(false),
  accuracy:     z.number().min(0).optional(),
  deviceId:     z.string().trim().max(200).optional(),
});

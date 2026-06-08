const R = 6371000; // Earth radius in metres

export function haversineDistance(lat1, lng1, lat2, lng2) {
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
    Math.cos((lat2 * Math.PI) / 180) *
    Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

export function isInsideGeofence(lat, lng, geofences) {
  return geofences.some(
    (g) => haversineDistance(lat, lng, g.lat, g.lng) <= (g.radiusM ?? 80)
  );
}

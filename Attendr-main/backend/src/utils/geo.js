const R = 6371000; // Earth radius in metres

export function haversineDistance(lat1, lon1, lat2, lon2) {
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
    Math.cos((lat2 * Math.PI) / 180) *
    Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

export function isWithinGeofence(empLat, empLon, gfLat, gfLon, radiusMeters) {
  return haversineDistance(empLat, empLon, gfLat, gfLon) <= radiusMeters;
}

// Returns the nearest matching geofence or null
export function findMatchingGeofence(empLat, empLon, geofences) {
  for (const gf of geofences) {
    const dist = haversineDistance(empLat, empLon, gf.latitude, gf.longitude);
    if (dist <= gf.radiusMeters) return { geofence: gf, distance: Math.round(dist) };
  }
  return null;
}

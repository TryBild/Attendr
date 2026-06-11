// All times in IST (UTC+5:30)
export function dayStart(date = new Date()) {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

export function dayEnd(date = new Date()) {
  const d = new Date(date);
  d.setHours(23, 59, 59, 999);
  return d;
}

export function monthRange(year, month) {
  // month: 1-12
  const start = new Date(year, month - 1, 1);
  const end   = new Date(year, month, 1);
  return { start, end };
}

export function daysInMonth(year, month) {
  return new Date(year, month, 0).getDate();
}

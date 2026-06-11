export interface HelpArticle {
  slug:    string;
  title:   string;
  icon:    string;
  steps:   string[];
  related: string[];
}

export const helpArticles: HelpArticle[] = [
  {
    slug:  "otp-not-received",
    title: "Why didn't I receive an OTP?",
    icon:  "MessageSquare",
    steps: [
      "Check that you entered the correct 10-digit mobile number starting with 6, 7, 8, or 9.",
      "Ensure your phone has network signal and is not in airplane mode or DND.",
      "Wait up to 2 minutes — SMS delivery can sometimes be delayed due to network congestion.",
      "Check your spam or blocked messages folder — some carriers block promotional SMS.",
      "If you still haven't received it, tap 'Resend OTP' after the 60-second timer expires.",
      "If the problem persists, contact your HR administrator or reach out to Attendr support.",
    ],
    related: ["invalid-team-id", "location-permission"],
  },
  {
    slug:  "invalid-team-id",
    title: "Invalid Team ID / Organization Not Found",
    icon:  "Building2",
    steps: [
      "Your Team ID is a unique code like 'ABC001' given to you by your HR or manager.",
      "Make sure you're entering the exact Team ID — it is case-insensitive but must match exactly.",
      "Do not add spaces before or after the Team ID.",
      "If you don't know your Team ID, ask your HR manager or company administrator.",
      "Your administrator can find the Team ID in the Attendr Admin panel under Company Settings.",
      "If your HR just registered the company, make sure they have completed the registration.",
    ],
    related: ["unable-to-join-org"],
  },
  {
    slug:  "unable-to-join-org",
    title: "Unable to Join Organization",
    icon:  "Users",
    steps: [
      "Ensure your mobile number has been added to the company by your HR/admin.",
      "Ask your HR to add you in the Employees section of the Admin panel.",
      "Check that your account has not been deactivated — contact HR if you see a deactivation message.",
      "Make sure you are using the same mobile number that was registered with HR.",
      "Try clearing your browser cache and reloading the app.",
      "If nothing works, contact Attendr support with your Team ID and mobile number.",
    ],
    related: ["invalid-team-id", "otp-not-received"],
  },
  {
    slug:  "changed-phone",
    title: "Changed Phone / New Device",
    icon:  "Smartphone",
    steps: [
      "Good news — Attendr works on any device! There is no device lock for employees.",
      "Simply open Attendr on your new phone and go through the normal registration process.",
      "Enter your Full Name, Mobile Number, and Team ID as usual.",
      "Request and verify your OTP — this will log you in on the new device automatically.",
      "Your attendance history will remain intact as it is stored in the cloud.",
      "If you face any issues, contact your HR manager.",
    ],
    related: ["otp-not-received"],
  },
  {
    slug:  "location-permission",
    title: "Location Permission Required",
    icon:  "MapPin",
    steps: [
      "Attendr needs your location to verify that you are within the office premises before marking attendance.",
      "On Android: Go to Settings → Apps → Browser (Chrome/Firefox) → Permissions → Location → Allow.",
      "On iOS/iPhone: Go to Settings → Privacy → Location Services → Safari → While Using the App.",
      "When you open Attendr and tap 'Check In', your browser will ask for location permission — tap Allow.",
      "If you accidentally denied permission, you must reset it in your phone settings (see steps above).",
      "Make sure you are physically present at the office when checking in — GPS must detect you within the defined area.",
    ],
    related: ["app-not-working"],
  },
  {
    slug:  "app-not-working",
    title: "App Not Working Properly",
    icon:  "RefreshCw",
    steps: [
      "First, check your internet connection — Attendr requires an active internet connection.",
      "Try refreshing the page by pulling down (mobile) or pressing F5 (desktop).",
      "Clear your browser cache: Settings → Privacy → Clear browsing data → Cached images/files.",
      "Try opening Attendr in a different browser (Chrome is recommended for best experience).",
      "If you see a blank screen, try force-closing the browser and reopening.",
      "If the problem persists, contact Attendr support with a description of the issue and a screenshot.",
    ],
    related: ["otp-not-received", "location-permission"],
  },
];

export function getArticle(slug: string): HelpArticle | undefined {
  return helpArticles.find((a) => a.slug === slug);
}

export function getRelatedArticles(slugs: string[]): HelpArticle[] {
  return slugs.map((s) => getArticle(s)).filter(Boolean) as HelpArticle[];
}

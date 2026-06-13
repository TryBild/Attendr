import { useLocation, Link } from "react-router-dom";
import { Home, Users, Calendar, BarChart3, User } from "lucide-react";

const TABS = [
  { label: "Home",      icon: Home,     to: "/admin/dashboard" },
  { label: "Employees", icon: Users,    to: "/admin/employees" },
  { label: "Calendar",  icon: Calendar, to: "/admin/calendar"  },
  { label: "Reports",   icon: BarChart3,to: "/admin/reports"   },
  { label: "Profile",   icon: User,     to: "/admin/profile"   },
] as const;

interface Props {
  children: React.ReactNode;
}

export function AdminLayout({ children }: Props) {
  const { pathname } = useLocation();

  return (
    <>
      {children}

      {/* Fixed bottom tab bar */}
      <nav className="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100"
           style={{ boxShadow: "0 -2px 12px rgba(0,0,0,0.06)" }}>
        <div className="flex max-w-2xl mx-auto">
          {TABS.map(({ label, icon: Icon, to }) => {
            const active = pathname === to;
            return (
              <Link
                key={to}
                to={to}
                className={`flex-1 flex flex-col items-center justify-center py-2.5 gap-0.5 relative transition-colors ${
                  active ? "text-[#1B3A7B]" : "text-gray-400"
                }`}
              >
                <Icon size={21} strokeWidth={active ? 2.2 : 1.8} />
                <span className="text-[10px] font-semibold">{label}</span>
                {active && (
                  <span className="absolute top-0 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-[#1B3A7B] rounded-full" />
                )}
              </Link>
            );
          })}
        </div>
      </nav>
    </>
  );
}

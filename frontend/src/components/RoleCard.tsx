import { ReactNode } from "react";
import { Check } from "lucide-react";

interface RoleCardProps {
  icon: ReactNode;
  title: string;
  description: string;
  selected: boolean;
  onPress: () => void;
}

export function RoleCard({ icon, title, description, selected, onPress }: RoleCardProps) {
  return (
    <button
      role="radio"
      aria-checked={selected}
      onClick={onPress}
      className={[
        "w-full text-left rounded-2xl p-5 transition-all duration-200 active:scale-[0.98]",
        selected
          ? "bg-[#EEF2FF] border-2 border-[#1E3A8A]"
          : "bg-white border-[1.5px] border-[#E2E8F0]",
      ].join(" ")}
    >
      <div className="flex items-center gap-[14px]">
        <div
          className={[
            "w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 transition-colors duration-200",
            selected ? "bg-[#1E3A8A]" : "bg-[#F1F5F9]",
          ].join(" ")}
        >
          <span className={`flex transition-colors duration-200 ${selected ? "text-white" : "text-[#64748B]"}`}>
            {icon}
          </span>
        </div>

        <div className="flex-1 min-w-0">
          <p className="text-base font-semibold text-[#0F172A] leading-tight">{title}</p>
          <p className="text-[13px] text-[#64748B] mt-[3px] leading-[1.5]">{description}</p>
        </div>

        <div
          className={[
            "w-[22px] h-[22px] rounded-full flex items-center justify-center flex-shrink-0 transition-all duration-200",
            selected ? "bg-[#1E3A8A]" : "bg-transparent border-[1.5px] border-[#E2E8F0]",
          ].join(" ")}
        >
          <Check
            size={14}
            color="white"
            strokeWidth={3}
            className={`transition-all duration-150 ${selected ? "opacity-100 scale-100" : "opacity-0 scale-0"}`}
          />
        </div>
      </div>
    </button>
  );
}

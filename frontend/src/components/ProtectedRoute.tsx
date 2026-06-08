import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import type { UserKind } from "../store/auth";

interface Props {
  kind: UserKind;
  children: React.ReactNode;
}

export function ProtectedRoute({ kind, children }: Props) {
  const { isAuthenticated, kind: userKind } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={`/login/${kind}`} replace />;
  }

  if (userKind !== kind) {
    return (
      <div className="flex items-center justify-center min-h-screen text-red-600">
        Access denied.
      </div>
    );
  }

  return <>{children}</>;
}

import type { NextConfig } from "next";

const backendUrl =
    process.env.BACKEND_INTERNAL_URL ?? "http://mentorlink-backend:8080";

const nextConfig: NextConfig = {
    output: "standalone",
    async rewrites() {
        return [
            {
                source: "/api/v1/:path*",
                destination: `${backendUrl}/api/v1/:path*`,
            },
        ];
    },
};

export default nextConfig;
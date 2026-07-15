import { QueryClient } from "@tanstack/react-query";

export function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {
                staleTime: 1000 * 60 * 5,
                gcTime: 1000 * 60 * 30,
                retry: 1,
                refetchOnWindowFocus: false,
            },
        },
    });
}
'use client'

import { QueryClientProvider } from "@tanstack/react-query";
import React, { useState } from "react";
import { createQueryClient } from "@/lib/react-query/queryClient";

export default function QueryProvider({ children }: {
    children: React.ReactNode;
}) {
    const [queryClient] = useState(createQueryClient);

    return (
        <QueryClientProvider client={queryClient}>
            {children}
        </QueryClientProvider>
    );
}
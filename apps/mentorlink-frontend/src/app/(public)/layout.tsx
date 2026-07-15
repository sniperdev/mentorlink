import { Navbar } from "@/components/layout/Navbar";
import React from "react";

export default function PublicLayout({children}: {
    children: React.ReactNode;
}) {
    return (
        <>
            <Navbar />

            <main>{children}</main>

            {/*<Footer />*/}
        </>
    );
}
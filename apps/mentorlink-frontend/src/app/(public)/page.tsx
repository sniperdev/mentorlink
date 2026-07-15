import {Navbar} from "@/components/layout/Navbar";
import HeroSection from "@/features/home/sections/HeroSection";
import SearchSection from "@/features/home/sections/SearchSection";


export default function HomePage() {
    return (
        <>
            <Navbar/>
            <HeroSection/>
            <SearchSection/>
        </>
    )
}
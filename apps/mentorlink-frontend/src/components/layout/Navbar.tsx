import {BookOpenText} from "lucide-react";
import { NavigationMenu , NavigationMenuList, NavigationMenuItem, NavigationMenuLink} from "@/components/ui/navigation-menu";
import { Button } from "@/components/ui/button";

export function Navbar() {
    return (
        <header className="border-b border-gray-200">
            <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
                <div className="flex items-center gap-2 cursor-pointer">
                    <div className="rounded-full p-2 bg-blue-400">
                        <BookOpenText className="text-white"/>
                    </div>
                    <p>MentorLink</p>
                </div>

                <NavigationMenu>
                    <NavigationMenuList>
                        <NavigationMenuItem>
                            <NavigationMenuLink>Home</NavigationMenuLink>
                        </NavigationMenuItem>
                        <NavigationMenuItem>
                            <NavigationMenuLink>Tutors</NavigationMenuLink>
                        </NavigationMenuItem>
                        <NavigationMenuItem>
                            <NavigationMenuLink>Subjects</NavigationMenuLink>
                        </NavigationMenuItem>
                    </NavigationMenuList>
                </NavigationMenu>

                <div className="flex items-center gap-3">
                    <Button>Log in</Button>
                    <Button variant="outline">Sign up</Button>
                </div>

            </div>
        </header>
    )
}
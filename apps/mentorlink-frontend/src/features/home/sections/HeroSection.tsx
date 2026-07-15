import { ArrowRight } from "lucide-react";

import {
    Avatar,
    AvatarFallback,
    AvatarGroup,
    AvatarGroupCount,
    AvatarImage,
} from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";

export default function HeroSection() {
    return (
        <section className="py-20 lg:py-28">
            <div className="mx-auto grid max-w-7xl items-center gap-20 px-6 lg:grid-cols-2">
                <div className="flex flex-col gap-8">
                    <Badge
                        variant="secondary"
                        className="w-fit rounded-full px-4 py-1 text-sm font-medium"
                    >
                        2,300+ expert tutors available now
                    </Badge>

                    <div className="space-y-6">
                        <h1 className="text-5xl font-bold leading-tight tracking-tight lg:text-7xl">
                            Find the perfect tutor for your learning goals.
                        </h1>

                        <p className="max-w-xl text-lg leading-8 text-muted-foreground">
                            Connect with experienced tutors for mathematics,
                            languages, programming and dozens of other
                            subjects. Learn at your pace, on your schedule.
                        </p>
                    </div>

                    <div className="flex items-center gap-4">
                        <Button
                            size="lg"
                            className="rounded-xl px-6 gap-2"
                        >
                            Find a tutor
                            <ArrowRight className="h-4 w-4" />
                        </Button>

                        <Button
                            size="lg"
                            variant="outline"
                            className="rounded-xl px-6"
                        >
                            Become a tutor
                        </Button>
                    </div>

                    <Separator />

                    <div className="flex items-center gap-4">
                        <AvatarGroup className="-space-x-3">
                            <Avatar className="h-11 w-11 border-2 border-background">
                                <AvatarImage
                                    src="https://github.com/shadcn.png"
                                    alt="@shadcn"
                                />
                                <AvatarFallback>CN</AvatarFallback>
                            </Avatar>

                            <Avatar className="h-11 w-11 border-2 border-background">
                                <AvatarImage
                                    src="https://github.com/maxleiter.png"
                                    alt="@maxleiter"
                                />
                                <AvatarFallback>LR</AvatarFallback>
                            </Avatar>

                            <Avatar className="h-11 w-11 border-2 border-background">
                                <AvatarImage
                                    src="https://github.com/evilrabbit.png"
                                    alt="@evilrabbit"
                                />
                                <AvatarFallback>ER</AvatarFallback>
                            </Avatar>

                            <AvatarGroupCount className="h-11 w-11 border-2 border-background">
                                +3
                            </AvatarGroupCount>
                        </AvatarGroup>

                        <div>
                            <p className="font-semibold">
                                Joined by 12,000+ students
                            </p>

                            <p className="text-sm text-muted-foreground">
                                Start learning with trusted tutors today.
                            </p>
                        </div>
                    </div>
                </div>

                <div className="h-155 rounded-[32px] bg-slate-100 shadow-xl" />
            </div>
        </section>
    );
}
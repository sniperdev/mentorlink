import { TutorSummary } from "../types/tutor";

import {
    Card,
    CardContent,
    CardFooter,
    CardHeader,
} from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import Link from "next/link";

interface TutorCardProps {
    tutor: TutorSummary;
}

export function TutorCard({ tutor }: TutorCardProps) {
    return (
        <Link href={`/tutors/${tutor.id}`}>
            <Card className="rounded-2xl">
                <CardHeader className="flex flex-row items-center gap-4">
                    <Avatar className="h-12 w-12">
                        <AvatarFallback>
                            {tutor.firstName[0]}
                            {tutor.lastName[0]}
                        </AvatarFallback>
                    </Avatar>

                    <div>
                        <h3 className="font-semibold">
                            {tutor.firstName} {tutor.lastName}
                        </h3>

                        <p className="text-sm text-muted-foreground">
                            {tutor.email}
                        </p>
                    </div>
                </CardHeader>

                <CardContent>
                    <p className="line-clamp-3 text-sm text-muted-foreground">
                        {tutor.bio}
                    </p>
                </CardContent>

                <CardFooter>
                    <Button className="w-full">
                        View Profile
                    </Button>
                </CardFooter>
            </Card>
        </Link>
    );
}
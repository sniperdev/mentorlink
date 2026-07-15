"use client";

import { useTutor } from "@/features/tutors/hooks";
import {useParams} from "next/navigation";

export default function TutorProfilePage() {
    const params = useParams<{id: string}>()
    const { data: tutor, isLoading } = useTutor(params.id);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (!tutor) {
        return <div>Tutor not found.</div>;
    }

    return (
        <div className="mx-auto max-w-7xl p-10">
            <h1 className="text-4xl font-bold">
                {tutor.firstName} {tutor.lastName}
            </h1>

            <p className="mt-4 text-muted-foreground">
                {tutor.email}
            </p>

            <p className="mt-8">{tutor.bio}</p>
        </div>
    );
}
"use client";

import { TutorCard } from "@/features/tutors/components/TutorCard";
import { useTutors } from "@/features/tutors/hooks";
import {TutorSummary} from "@/features/tutors/types/tutor";

export default function FeaturedTutorsSection() {
    const { data: tutors, isLoading } = useTutors();

    if (isLoading) {
        return (
            <section className="pb-24">
                <div className="mx-auto max-w-7xl px-6">
                    <p>Loading tutors...</p>
                </div>
            </section>
        );
    }

    return (
        <section className="pb-24">
            <div className="mx-auto max-w-7xl px-6">

                <div className="mb-10 flex items-end justify-between">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.25em] text-slate-400">
                            Featured tutors
                        </p>

                        <h2 className="mt-2 text-4xl font-bold tracking-tight">
                            Find your tutor
                        </h2>
                    </div>
                </div>

                <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                    {tutors?.map((tutor: TutorSummary) => (
                        <TutorCard
                            key={tutor.id}
                            tutor={tutor}
                        />
                    ))}
                </div>

            </div>
        </section>
    );
}
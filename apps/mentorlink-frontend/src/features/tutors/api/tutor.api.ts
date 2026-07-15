import {TutorSummary} from "@/features/tutors/types/tutor";
import {apiClient} from "@/lib/api/client";


export async function getTutors(): Promise<TutorSummary[]> {
    const response = await apiClient.get<TutorSummary[]>("/tutors");

    return response.data;
}

export async function getTutor(id: string): Promise<TutorSummary> {
    const response = await apiClient.get<TutorSummary>(`/tutors/${id}`);

    return response.data;
}
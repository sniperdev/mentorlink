import {apiClient} from "@/lib/api/client";


export async function getTutors() {
    const response = await apiClient.get('/tutors')
    return response.data;
}
import {useQuery} from "@tanstack/react-query";
import {queryKeys} from "@/lib/react-query/queryKeys";
import {getTutor, getTutors} from "@/features/tutors/api";


export function useTutors() {
    return useQuery({
        queryKey: queryKeys.tutors,
        queryFn: getTutors,
    })
}

export function useTutor(id: string) {
    return useQuery({
        queryKey: queryKeys.tutor(id),
        queryFn: () => getTutor(id),
        enabled: !!id,
    })
}
import {useQuery} from "@tanstack/react-query";
import {getTutors} from "@/features/api/tutor.api";
import {queryKeys} from "@/lib/react-query/queryKeys";


export function useTutors() {
    return useQuery({
        queryKey: queryKeys.tutors,
        queryFn: getTutors,
    })
}
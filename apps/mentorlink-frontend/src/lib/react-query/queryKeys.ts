
export const queryKeys = {
    tutors: ['tutors'],
    tutor: (id: string) => ['tutors', id] as const,
}
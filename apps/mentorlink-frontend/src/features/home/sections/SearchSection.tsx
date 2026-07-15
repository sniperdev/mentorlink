import { Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";

export default function SearchSection() {
    return (
        <section className="-mt-8 pb-24">
            <div className="mx-auto max-w-7xl px-6">
                <div className="rounded-[32px] border bg-white p-8 shadow-sm">

                    <p className="mb-6 text-xs font-semibold uppercase tracking-[0.25em] text-slate-400">
                        Search for a tutor
                    </p>

                    <div className="grid items-end gap-6 lg:grid-cols-[1.2fr_1.2fr_0.8fr_auto]">

                        <div className="space-y-2">
                            <label className="text-sm font-medium">
                                Subject
                            </label>

                            <Select>
                                <SelectTrigger className="h-12 rounded-2xl">
                                    <SelectValue placeholder="Any subject" />
                                </SelectTrigger>

                                <SelectContent>
                                    <SelectItem value="math">
                                        Mathematics
                                    </SelectItem>

                                    <SelectItem value="english">
                                        English
                                    </SelectItem>

                                    <SelectItem value="programming">
                                        Programming
                                    </SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-medium">
                                Location
                            </label>

                            <Input
                                className="h-12 rounded-2xl"
                                placeholder="City or region"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-medium">
                                Online only
                            </label>

                            <div className="flex h-12 items-center gap-3 rounded-2xl border px-4">
                                <Switch />
                                <span className="text-sm text-muted-foreground">
                                    Any format
                                </span>
                            </div>
                        </div>

                        <Button
                            size="lg"
                            className="h-12 rounded-2xl px-8"
                        >
                            <Search className="mr-2 h-4 w-4" />
                            Search
                        </Button>

                    </div>
                </div>
            </div>
        </section>
    );
}
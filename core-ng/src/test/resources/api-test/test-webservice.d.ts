namespace core.framework.impl.web.service {
    export interface TestWebService$TestSearchRequest {
        int_field: number;
        boolean_field?: boolean;
        long_field?: number;
        double_field?: number;
        date_field?: Date;
    }

    export interface TestWebService$TestResponse {
        int_field?: number;
        string_map?: { [key: string]: string; };
        items?: { [key: string]: core.framework.impl.web.service.TestWebService$TestItem; };
    }

    export interface TestWebService$TestItem {
        zoned_date_time_field?: Date;
        enum_field?: TestWebService$TestEnum;
    }

    export interface TestWebService$TestRequest {
        string_field: string;
        items?: core.framework.impl.web.service.TestWebService$TestItem[];
    }

    export enum TestWebService$TestEnum {
        A, B
    }

    export const testWebService = {
        search: {method: "GET", path: "/test"},
        batch: {method: "PUT", path: "/test"},
        get: {method: "GET", path: "/test/:id"},
        create: {method: "PUT", path: "/test/:id"},
        delete: {method: "DELETE", path: "/test/:id"},
        patch: {method: "PATCH", path: "/test/:id"},
    };

    export interface TestWebService {
        search: (request: core.framework.impl.web.service.TestWebService$TestSearchRequest) => Promise<core.framework.impl.web.service.TestWebService$TestResponse>;
        batch: (request: core.framework.impl.web.service.TestWebService$TestRequest[]) => Promise<core.framework.impl.web.service.TestWebService$TestResponse[]>;
        get: (id: number) => Promise<core.framework.impl.web.service.TestWebService$TestResponse | null>;
        create: (id: number, request: core.framework.impl.web.service.TestWebService$TestRequest) => Promise<void>;
        delete: (id: string) => Promise<void>;
        patch: (id: number, request: core.framework.impl.web.service.TestWebService$TestRequest) => Promise<void>;
    }
}

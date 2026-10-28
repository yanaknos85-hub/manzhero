import { DepartmentDetailedModel } from '../models/DepartmentDetailed.model';
import { ICorporateFiltersStore, TDepartmentFiltersTitles } from './CorporateFilters.interface';
export declare class DICorporateFiltersStore implements ICorporateFiltersStore {
    private mappedStore;
    filters: Map<keyof import("./CorporateFilters.interface").TDepartmentFilters, string>;
    get departmentsFiltered(): DepartmentDetailedModel[];
    editFilteres(filters: Map<TDepartmentFiltersTitles, string>): void;
    refreshFilteres(): void;
}

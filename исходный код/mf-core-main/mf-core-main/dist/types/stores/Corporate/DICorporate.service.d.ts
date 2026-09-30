import { ICorporateService, IOrganization, IPosition, TDepartment } from './Corporate.interface';
import { DepartmentDetailedModel } from './models/DepartmentDetailed.model';
export declare class DICorporateService implements ICorporateService {
    private http;
    private process;
    getAllOrganizations(): Promise<IOrganization[]>;
    getAllDepartments(orgId: string): Promise<{
        content: TDepartment[];
    }>;
    getAllPositions(orgId: string): Promise<IPosition[]>;
    getOrganization(orgId: string): Promise<IOrganization>;
    getDepartment(orgId: string, depId: string): Promise<TDepartment>;
    addDepartment(orgId: string, model: DepartmentDetailedModel): Promise<TDepartment>;
    editDepartment(orgId: string, depId: string, model: DepartmentDetailedModel): Promise<number>;
    deleteDepartment(orgId: string, depId: string): Promise<number>;
    getPosition(orgId: string, posId: string): Promise<IPosition>;
}

import mapKeys from 'lodash/mapKeys';
var OrganizationNormalizedModel = /** @class */ (function () {
    function OrganizationNormalizedModel(org) {
        this.id = org.id;
        this.officialName = org.officialName;
        this.address = org.address;
        this.positions = mapKeys(org.positions, 'id');
        this.departments = mapKeys(org.departments, 'id');
    }
    return OrganizationNormalizedModel;
}());
export { OrganizationNormalizedModel };

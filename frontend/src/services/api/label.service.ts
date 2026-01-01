import { BaseApiService } from './base.service';
import { 
  Label, 
  CreateLabelRequest, 
  UpdateLabelRequest,
  PageRequest,
  PageResponse 
} from '@/types';

class LabelApiService extends BaseApiService<Label, CreateLabelRequest, UpdateLabelRequest> {
  protected baseUrl = '/labels';

  /**
   * Get labels with filtering and pagination
   */
  async getLabels(params?: PageRequest & {
    search?: string;
  }): Promise<PageResponse<Label>> {
    return this.getAll(params);
  }

  /**
   * Get all labels (without pagination) for dropdowns
   */
  async getAllLabels(): Promise<Label[]> {
    const response = await this.getLabels({ size: 1000 });
    return response.content;
  }

  /**
   * Get label by ID
   */
  async getLabel(id: number): Promise<Label> {
    return this.getById(id);
  }

  /**
   * Create new label
   */
  async createLabel(data: CreateLabelRequest): Promise<Label> {
    return this.create(data);
  }

  /**
   * Update existing label
   */
  async updateLabel(id: number, data: UpdateLabelRequest): Promise<Label> {
    return this.update(id, data);
  }

  /**
   * Delete label
   */
  async deleteLabel(id: number): Promise<void> {
    return this.delete(id);
  }

  /**
   * Check if label name is available
   */
  async checkLabelNameAvailability(name: string): Promise<{ available: boolean }> {
    try {
      const labels = await this.getAllLabels();
      const nameExists = labels.some(label => 
        label.name.toLowerCase() === name.toLowerCase()
      );
      return { available: !nameExists };
    } catch (error) {
      console.warn('Label name availability check failed:', error);
      return { available: true };
    }
  }
}

export const labelService = new LabelApiService();
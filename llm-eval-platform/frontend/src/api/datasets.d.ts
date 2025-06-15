declare module '@/api/datasets' {
  export function getDatasetList(params: any): Promise<any>;
  export function getDatasetById(datasetId: number): Promise<any>;
  export function getDatasetQuestions(params: any): Promise<any>;
  export function getQuestionAnswers(params: any): Promise<any>;
  export function getDatasetStatistics(datasetId: number): Promise<any>;
  export function createDataset(data: any): Promise<any>;
  export function updateDataset(datasetId: number, data: any): Promise<any>;
  export function deleteDataset(datasetId: number): Promise<any>;
  export function importDataset(file: File): Promise<any>;
  export function exportDataset(datasetId: number, format?: string): Promise<any>;
} 
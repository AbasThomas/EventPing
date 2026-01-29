'use client';

import { useState } from 'react';
import { Plus, X, GripVertical, Type, Mail, Phone, ChevronDown, CheckSquare } from 'lucide-react';

export type FieldType = 'TEXT' | 'EMAIL' | 'PHONE' | 'SELECT' | 'CHECKBOX';

export interface CustomField {
  id: string;
  fieldName: string;
  fieldType: FieldType;
  required: boolean;
  placeholderText: string;
  fieldOptions: string; // Comma-separated for SELECT
  displayOrder: number;
}

interface CustomFieldBuilderProps {
  fields: CustomField[];
  onChange: (fields: CustomField[]) => void;
}

const FIELD_TYPES: { value: FieldType; label: string; icon: React.ComponentType<{ className?: string }> }[] = [
  { value: 'TEXT', label: 'Short Text', icon: Type },
  { value: 'EMAIL', label: 'Email Address', icon: Mail },
  { value: 'PHONE', label: 'Phone Number', icon: Phone },
  { value: 'SELECT', label: 'Dropdown', icon: ChevronDown },
  { value: 'CHECKBOX', label: 'Checkbox', icon: CheckSquare },
];

export function CustomFieldBuilder({ fields, onChange }: CustomFieldBuilderProps) {
  const [editingId, setEditingId] = useState<string | null>(null);

  const addField = () => {
    const newField: CustomField = {
      id: `field-${Date.now()}`,
      fieldName: '',
      fieldType: 'TEXT',
      required: false,
      placeholderText: '',
      fieldOptions: '',
      displayOrder: fields.length,
    };
    onChange([...fields, newField]);
    setEditingId(newField.id);
  };

  const removeField = (id: string) => {
    onChange(fields.filter(f => f.id !== id).map((f, index) => ({ ...f, displayOrder: index })));
  };

  const updateField = (id: string, updates: Partial<CustomField>) => {
    onChange(fields.map(f => f.id === id ? { ...f, ...updates } : f));
  };

  const moveField = (id: string, direction: 'up' | 'down') => {
    const index = fields.findIndex(f => f.id === id);
    if ((direction === 'up' && index === 0) || (direction === 'down' && index === fields.length - 1)) {
      return;
    }

    const newFields = [...fields];
    const swapIndex = direction === 'up' ? index - 1 : index + 1;
    [newFields[index], newFields[swapIndex]] = [newFields[swapIndex], newFields[index]];
    onChange(newFields.map((f, idx) => ({ ...f, displayOrder: idx })));
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold text-white mb-1">Custom Registration Fields</h3>
          <p className="text-sm text-slate-400">
            Add custom fields to collect additional information from participants
          </p>
        </div>
        <button
          type="button"
          onClick={addField}
          className="flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors text-sm font-medium"
        >
          <Plus className="w-4 h-4" />
          Add Field
        </button>
      </div>

      {fields.length === 0 ? (
        <div className="text-center py-12 border-2 border-dashed border-slate-800 rounded-xl">
          <Type className="w-12 h-12 text-slate-700 mx-auto mb-3" />
          <p className="text-slate-500 text-sm">No custom fields yet</p>
          <p className="text-slate-600 text-xs mt-1">Click "Add Field" to create your first custom field</p>
        </div>
      ) : (
        <div className="space-y-3">
          {fields.map((field, index) => (
            <div
              key={field.id}
              className="p-4 rounded-xl bg-slate-900/50 border border-slate-800 space-y-3"
            >
              <div className="flex items-start gap-3">
                <div className="flex flex-col gap-1 mt-2">
                  <button
                    type="button"
                    onClick={() => moveField(field.id, 'up')}
                    disabled={index === 0}
                    className="text-slate-600 hover:text-slate-400 disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    <GripVertical className="w-4 h-4" />
                  </button>
                </div>

                <div className="flex-1 space-y-3">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div className="space-y-1">
                      <label className="text-xs text-slate-400">Field Name</label>
                      <input
                        type="text"
                        value={field.fieldName}
                        onChange={(e) => updateField(field.id, { fieldName: e.target.value })}
                        placeholder="e.g. Dietary Preferences"
                        className="w-full px-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                      />
                    </div>

                    <div className="space-y-1">
                      <label className="text-xs text-slate-400">Field Type</label>
                      <select
                        value={field.fieldType}
                        onChange={(e) => updateField(field.id, { fieldType: e.target.value as FieldType })}
                        className="w-full px-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                      >
                        {FIELD_TYPES.map(type => (
                          <option key={type.value} value={type.value}>{type.label}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="space-y-1">
                    <label className="text-xs text-slate-400">Placeholder Text</label>
                    <input
                      type="text"
                      value={field.placeholderText}
                      onChange={(e) => updateField(field.id, { placeholderText: e.target.value })}
                      placeholder="Enter placeholder text..."
                      className="w-full px-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                    />
                  </div>

                  {field.fieldType === 'SELECT' && (
                    <div className="space-y-1">
                      <label className="text-xs text-slate-400">Options (comma-separated)</label>
                      <input
                        type="text"
                        value={field.fieldOptions}
                        onChange={(e) => updateField(field.id, { fieldOptions: e.target.value })}
                        placeholder="e.g. Small, Medium, Large, X-Large"
                        className="w-full px-3 py-2 bg-slate-900/50 border border-slate-700/50 rounded-lg text-slate-200 text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                      />
                    </div>
                  )}

                  <label className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={field.required}
                      onChange={(e) => updateField(field.id, { required: e.target.checked })}
                      className="w-4 h-4 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-800"
                    />
                    Required field
                  </label>
                </div>

                <button
                  type="button"
                  onClick={() => removeField(field.id)}
                  className="p-1.5 text-slate-600 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

'use client';

import { useState, useEffect, useRef } from 'react';
import { ChevronDown, Search, Check } from 'lucide-react';
import { apiFetch } from '@/lib/api';

interface Country {
  name: { common: string };
  cca2: string;
  idd: { root: string; suffixes?: string[] };
  flags: { svg: string; png: string; alt: string };
}

interface CountrySelectorProps {
  onSelect: (country: { code: string; dialCode: string; flag: string }) => void;
  selectedDialCode?: string;
  className?: string;
}

export function CountrySelector({ onSelect, selectedDialCode = '+1', className = '' }: CountrySelectorProps) {
  const [countries, setCountries] = useState<Country[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [selectedCountry, setSelectedCountry] = useState<Country | null>(null);
  const [loading, setLoading] = useState(true);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const response = await fetch('https://restcountries.com/v3.1/all?fields=name,cca2,idd,flags');
        if (!response.ok) throw new Error('Failed to fetch countries');
        const data = await response.json();
        
        // Filter countries that have valid idd data
        const validCountries = data.filter((c: Country) => c.idd?.root).sort((a: Country, b: Country) => 
          a.name.common.localeCompare(b.name.common)
        );
        
        setCountries(validCountries);

        // Set initial country based on selectedDialCode or default to US
        const initial = validCountries.find((c: Country) => {
            const code = c.idd.root + (c.idd.suffixes?.[0] || '');
            return code === selectedDialCode;
        }) || validCountries.find((c: Country) => c.cca2 === 'US');

        if (initial) {
             setSelectedCountry(initial);
             // Ensure parent is updated with initial if needed, but usually parent drives selectedDialCode
        }

        setLoading(false);
      } catch (error) {
        console.error('Error fetching countries:', error);
        setLoading(false);
      }
    };

    fetchCountries();
  }, []); // Only run once on mount

  // Sync internal state with external prop if it changes and we have countries
  useEffect(() => {
      if (countries.length > 0 && selectedDialCode) {
         const matching = countries.find(c => {
             const code = c.idd.root + (c.idd.suffixes?.[0] || '');
             return code === selectedDialCode;
         });
         if (matching) setSelectedCountry(matching);
      }
  }, [selectedDialCode, countries]);


  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getDialCode = (country: Country) => {
    if (!country.idd.root) return '';
    // Use the first suffix if available, or just root
    const suffix = country.idd.suffixes?.length ? country.idd.suffixes[0] : '';
    // Clean up if suffix is too long (some countries have many suffixes) - typically we just want the main one
    // For simplicity validation, taking first suffix is standard for simple selectors
    return `${country.idd.root}${suffix}`;
  };

  const filteredCountries = countries.filter(country => 
    country.name.common.toLowerCase().includes(search.toLowerCase()) ||
    getDialCode(country).includes(search)
  );

  const handleSelect = (country: Country) => {
    const dialCode = getDialCode(country);
    setSelectedCountry(country);
    onSelect({
      code: country.cca2,
      dialCode,
      flag: country.flags.svg
    });
    setIsOpen(false);
    setSearch('');
  };

  return (
    <div className={`relative ${className}`} ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-[100px] sm:w-[120px] flex items-center justify-between gap-2 px-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 hover:border-indigo-500/50 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
      >
        {selectedCountry ? (
          <div className="flex items-center gap-2 overflow-hidden">
            <img 
              src={selectedCountry.flags.svg} 
              alt={selectedCountry.name.common}
              className="w-5 h-3.5 object-cover rounded-[2px]" 
            />
            <span className="text-sm font-medium truncate">
              {getDialCode(selectedCountry)}
            </span>
          </div>
        ) : (
          <span className="text-slate-400 text-sm">Code</span>
        )}
        <ChevronDown className={`w-4 h-4 text-slate-500 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-2 w-[300px] max-h-[300px] bg-slate-900 border border-slate-700 rounded-xl shadow-2xl z-50 flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-100">
          <div className="p-2 border-b border-slate-800 sticky top-0 bg-slate-900 z-10">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search country..."
                className="w-full bg-slate-800 rounded-lg pl-9 pr-3 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                autoFocus
              />
            </div>
          </div>
          
          <div className="overflow-y-auto flex-1 p-1 scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent">
            {loading ? (
              <div className="p-4 text-center text-slate-500 text-sm">Loading countries...</div>
            ) : filteredCountries.length === 0 ? (
              <div className="p-4 text-center text-slate-500 text-sm">No countries found</div>
            ) : (
              filteredCountries.map((country) => (
                <button
                  key={country.cca2}
                  type="button"
                  onClick={() => handleSelect(country)}
                  className="w-full flex items-center justify-between p-2 rounded-lg hover:bg-indigo-500/20 hover:text-indigo-300 text-slate-300 transition-colors text-left"
                >
                  <div className="flex items-center gap-3 overflow-hidden">
                    <img 
                      src={country.flags.svg} 
                      alt={country.name.common}
                      className="w-6 h-4 object-cover rounded-[2px]" 
                    />
                    <span className="text-sm truncate">{country.name.common}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-sm text-slate-500 font-mono">{getDialCode(country)}</span>
                    {selectedCountry?.cca2 === country.cca2 && (
                         <Check className="w-3.5 h-3.5 text-indigo-400" />
                    )}
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

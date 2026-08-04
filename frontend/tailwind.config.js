/** @type {import('tailwindcss').Config} */
export default {
    darkMode: 'class',
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    safelist: [
        {
            pattern: /bg-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
        {
            pattern: /text-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
        {
            pattern: /border-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
        {
            pattern: /ring-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
        {
            pattern: /from-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
        {
            pattern: /to-(theme|aided|neutral)-(50|100|200|300|400|500|600|700|800|900|950)/,
        },
    ],
    theme: {
        extend: {
            colors: {
                theme: {
                    50: 'var(--theme-50)',
                    100: 'var(--theme-100)',
                    200: 'var(--theme-200)',
                    300: 'var(--theme-300)',
                    400: 'var(--theme-400)',
                    500: 'var(--theme-500)',
                    600: 'var(--theme-600)',
                    700: 'var(--theme-700)',
                    800: 'var(--theme-800)',
                    900: 'var(--theme-900)',
                    950: 'var(--theme-950)',
                },
                aided: {
                    50: 'var(--aided-50)',
                    100: 'var(--aided-100)',
                    200: 'var(--aided-200)',
                    300: 'var(--aided-300)',
                    400: 'var(--aided-400)',
                    500: 'var(--aided-500)',
                    600: 'var(--aided-600)',
                    700: 'var(--aided-700)',
                    800: 'var(--aided-800)',
                    900: 'var(--aided-900)',
                    950: 'var(--aided-950)',
                },
                neutral: {
                    50: 'var(--neutral-50)',
                    100: 'var(--neutral-100)',
                    200: 'var(--neutral-200)',
                    300: 'var(--neutral-300)',
                    400: 'var(--neutral-400)',
                    500: 'var(--neutral-500)',
                    600: 'var(--neutral-600)',
                    700: 'var(--neutral-700)',
                    800: 'var(--neutral-800)',
                    900: 'var(--neutral-900)',
                    950: 'var(--neutral-950)',
                },
                success: 'var(--color-success)',
                warning: 'var(--color-warning)',
                error: 'var(--color-error)',
                info: 'var(--color-info)',
                base: 'var(--bg-base)',
                elevated: 'var(--bg-elevated)',
                muted: 'var(--text-muted)',
            },
        },
    },
    plugins: [],
}

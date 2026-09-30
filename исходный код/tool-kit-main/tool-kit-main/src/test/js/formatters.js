const YEAR_MONTH_COUNT = 12;
const YEARS = ['год', 'года', 'лет'];
const MONTH = 'мес';

/**
 * Форматирует срок
 * @param {number} term - срок в месяцах
 * @returns {string} - годы и месяцы
 */
export function formatTerm(term) {
  const years = Math.floor(term / YEAR_MONTH_COUNT);
  const months = term % YEAR_MONTH_COUNT;

  return (
    (years ? `${years} ${pluralize(years, YEARS)}` : '') +
    ((years && months && ' ') || '') +
    (months ? `${months} ${MONTH}` : '')
  );
}

export function pluralize(value, [one, two, five]) {
  let number = Math.abs(value) % 100;

  if (number >= 5 && number <= 20) {
    return five;
  }

  number %= 10;

  if (number === 1) {
    return one;
  }
  if (number >= 2 && number <= 4) {
    return two;
  }

  return five;
}

export function formatAmount(amount = 0, decimals = false, delimiter = ' ') {
  const escapedAmount = amount.toString().replace(/\s/g, '');
  const parts = parseFloat(escapedAmount).toFixed(2).split('.');

  return (
    (parseInt(parts[0], 10) || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, delimiter) +
    (decimals ? `,${parts[1] || '00'}` : '').replace(/0$/, '')
  );
}

export function intervalsCreator(min, max, step) {
  const intervals = [];

  intervals.push(min);

  if (step) {
    for (let i = min + step; i < max; i += step) {
      intervals.push(Math.floor(i / step) * step);
    }
  }

  intervals.push(max);

  return intervals;
}

export function formatAmountExtra(amount = 0) {
  const output = [];

  if (amount / 1000000 >= 1) {
    output.push(`${Math.floor(amount / 1000000)} млн`);
  }

  // prettier-ignore
  // eslint-disable-next-line no-mixed-operators
  if (amount % 1000000 / 1000 >= 1) {
        // eslint-disable-next-line no-mixed-operators
        output.push(`${Math.floor(amount % 1000000 / 1000)} тыс`);
    }
  // prettier-ignore-end

  return output.join(' ') || amount.toString();
}

export const testBracketSpacing = ({ param1 = 1, param2 = 2 } = {}) => param1 + param2;
